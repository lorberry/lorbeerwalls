package de.laurel.lorbeerwalls.hacks;

import de.laurel.lorbeerwalls.process.GameProcess;
import de.laurel.lorbeerwalls.sdk.EnemyData;
import de.laurel.lorbeerwalls.sdk.Offsets;
import de.laurel.lorbeerwalls.structs.Vector3;
import de.laurel.lorbeerwalls.ui.MainGUI;
import de.laurel.lorbeerwalls.ui.WallhackOverlay;
import de.laurel.lorbeerwalls.utils.MathUtils;
import de.laurel.lorbeerwalls.utils.LorbeerKernelDriver;

import javax.swing.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * SwingWorker for handling the wallhack logic in background
 * @author lorberry+chatgpt
 */
public class WallhackWorker extends SwingWorker<Void, Object> {

    /** main user interface instance */
    private final MainGUI mainGUI;
    /** overlay window for drawing */
    private final WallhackOverlay overlay;
    /** game process handler */
    private GameProcess workerGameProcess;

    /**
     * creates a new wallhack worker
     * @param mainGUI the main gui
     * @param overlay the overlay to draw on
     */
    public WallhackWorker(MainGUI mainGUI, WallhackOverlay overlay) {
        this.mainGUI = mainGUI;
        this.overlay = overlay;
    }

    /**
     * main loop for reading game data
     * @return null
     * @throws Exception on error
     */
    @Override
    protected Void doInBackground() throws Exception {
        publish("initializing gameprocess");
        workerGameProcess = new GameProcess(this::publish);

        if (!workerGameProcess.isProcessAttached()) {
            throw new IllegalStateException("cs2 exe not found or failed to attach");
        }
        publish("gameprocess attached starting data loop");

        Offsets offsets = workerGameProcess.getOffsets();
        LorbeerKernelDriver memory = workerGameProcess.getMemoryReader();
        long clientBase = workerGameProcess.getClientModuleBase();
        float[] viewMatrix = new float[16];

        while (!isCancelled()) {
            List<EnemyData> currentFrameEnemies = new ArrayList<>();

            com.sun.jna.Pointer viewMatrixPtr = memory.readMemory(clientBase + offsets.dwViewMatrix, 16 * 4);
            if (viewMatrixPtr == null) {
                Thread.sleep(16);
                continue;
            }
            viewMatrixPtr.read(0, viewMatrix, 0, 16);

            long localPlayerPawn = memory.readLong(clientBase + offsets.dwLocalPlayerPawn);
            if (localPlayerPawn == 0) {
                Thread.sleep(16);
                continue;
            }
            int localTeam = memory.readInt(localPlayerPawn + offsets.m_iTeamNum);

            long pEntityList = memory.readLong(clientBase + offsets.dwEntityList);
            if (pEntityList == 0) {
                Thread.sleep(16);
                continue;
            }

            for (int i = 1; i <= 64; i++) {
                long listEntry = memory.readLong(pEntityList + (8 * (i >> 9) + 16));
                if (listEntry == 0) continue;
                long playerController = memory.readLong(listEntry + 120L * (i & 0x1FF));
                if (playerController == 0) continue;

                long playerPawnHandle = memory.readLong(playerController + offsets.m_hPlayerPawn);
                if (playerPawnHandle == 0) continue;

                long listEntry2 = memory.readLong(pEntityList + (8 * ((playerPawnHandle & 0x7FFF) >> 9) + 16));
                if (listEntry2 == 0) continue;
                long entityPawn = memory.readLong(listEntry2 + 120L * (playerPawnHandle & 0x1FF));
                if (entityPawn == 0 || entityPawn == localPlayerPawn) continue;

                int health = memory.readInt(entityPawn + offsets.m_iHealth);
                if (health <= 0 || health > 100) continue;

                int team = memory.readInt(entityPawn + offsets.m_iTeamNum);
                if (team == localTeam) continue;

                Vector3 enemyPos = memory.readVector3(entityPawn + offsets.m_vOldOrigin);
                if (enemyPos == null) continue;

                Point2D.Float screenPos = MathUtils.worldToScreen(enemyPos, viewMatrix, overlay.getWidth(), overlay.getHeight());
                if (screenPos != null) {
                    currentFrameEnemies.add(new EnemyData(screenPos, health));
                }
            }

            publish(currentFrameEnemies);
            Thread.sleep(1000 / 120);
        }
        return null;
    }

    /**
     * updates gui with data from background thread
     * @param chunks data chunks to process
     */
    @Override
    protected void process(List<Object> chunks) {
        for (Object chunk : chunks) {
            if (chunk instanceof String message) {
                mainGUI.printToLog(message);
            } else if (chunk instanceof List<?> list) {
                if (overlay != null) {
                    @SuppressWarnings("unchecked")
                    List<EnemyData> enemyData = (List<EnemyData>) list;
                    overlay.updateEnemyData(enemyData);
                }
            }
        }
    }

    /**
     * cleans up resources after worker finishes
     */
    @Override
    protected void done() {
        try {
            get();
        } catch (InterruptedException | ExecutionException e) {
            mainGUI.printToLog("error: " + e.getCause().getMessage());
            e.getCause().printStackTrace();
        } catch (java.util.concurrent.CancellationException e) {
            mainGUI.printToLog("wallhack stopped");
        } finally {
            if (workerGameProcess != null && workerGameProcess.isProcessAttached()) {
                workerGameProcess.detach();
            }
            mainGUI.onWallhackStopped();
        }
    }
}

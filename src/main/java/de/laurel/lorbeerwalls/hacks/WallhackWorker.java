package de.laurel.lorbeerwalls.hacks;

import com.sun.jna.Pointer;
import de.laurel.lorbeerwalls.process.GameProcess;
import de.laurel.lorbeerwalls.sdk.EnemyData;
import de.laurel.lorbeerwalls.sdk.Offsets;
import de.laurel.lorbeerwalls.structs.Vector3;
import de.laurel.lorbeerwalls.ui.MainGUI;
import de.laurel.lorbeerwalls.ui.WallhackOverlay;
import de.laurel.lorbeerwalls.utils.LorbeerKernelDriver;
import de.laurel.lorbeerwalls.utils.MathUtils;

import javax.swing.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class WallhackWorker extends SwingWorker<Void, Object> {

    private final MainGUI mainGUI;
    private final WallhackOverlay overlay;
    private GameProcess workerGameProcess;

    public static final int[][] BONE_CONNECTIONS = {
            {6, 5}, {5, 4}, {4, 3}, {3, 0},
            {4, 8}, {8, 9}, {9, 10},
            {4, 13}, {13, 14}, {14, 15},
            {0, 22}, {22, 23}, {23, 24},
            {0, 26}, {26, 27}, {27, 28}
    };

    public WallhackWorker(MainGUI mainGUI, WallhackOverlay overlay) {
        this.mainGUI = mainGUI;
        this.overlay = overlay;
    }

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

            Pointer viewMatrixPtr = memory.readMemory(clientBase + offsets.dwViewMatrix, 16 * 4);
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
                //if (team == localTeam) continue;

                long gameSceneNode = memory.readLong(entityPawn + offsets.m_pGameSceneNode);
                if (gameSceneNode == 0) continue;

                long boneMatrixAddress = memory.readLong(gameSceneNode + offsets.m_modelState + 0x80);
                if (boneMatrixAddress == 0) continue;

                Pointer boneMatrixPtr = memory.readMemory(boneMatrixAddress, 30 * 32);
                if (boneMatrixPtr == null) continue;

                Map<Integer, Point2D.Float> bonePositions = new HashMap<>();
                for (int boneId = 0; boneId < 30; boneId++) {
                    float x = boneMatrixPtr.getFloat((long) boneId * 32);
                    float y = boneMatrixPtr.getFloat((long) boneId * 32 + 4);
                    float z = boneMatrixPtr.getFloat((long) boneId * 32 + 8);
                    Vector3 bonePos = new Vector3(x,y,z);
                    Point2D.Float screenPos = MathUtils.worldToScreen(bonePos, viewMatrix, overlay.getWidth(), overlay.getHeight());
                    if (screenPos != null) {
                        bonePositions.put(boneId, screenPos);
                    }
                }

                if(!bonePositions.isEmpty()){
                    currentFrameEnemies.add(new EnemyData(bonePositions, health));
                }
            }

            publish(currentFrameEnemies);
            Thread.sleep(1000 / 144);
        }
        return null;
    }

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

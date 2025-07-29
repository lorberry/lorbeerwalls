package de.laurel.lorbeerwalls.process;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import de.laurel.lorbeerwalls.sdk.Offsets;
import de.laurel.lorbeerwalls.utils.LorbeerKernelDriver;
import de.laurel.lorbeerwalls.utils.ProcessFinder;

import java.util.function.Consumer;

/**
 * manages attaching to the game process and reading memory
 * @author lorberry+chatgpt
 */
public class GameProcess {
    /** name of the target process */
    private static final String PROCESS_NAME = "cs2.exe";
    /** consumer for logging messages */
    private final Consumer<String> logger;
    /** game offsets container */
    private final Offsets offsets;
    /** handle to the game process */
    private WinNT.HANDLE processHandle;
    /** base address of client dll */
    private long clientModuleBase;
    /** memory reading driver */
    private LorbeerKernelDriver lorbeerKernelDriver;

    /**
     * creates a game process handler
     * @param logger logger to output messages
     */
    public GameProcess(Consumer<String> logger) {
        this.logger = logger;
        this.offsets = new Offsets(logger);
        if (offsets.load()) {
            attachToProcess();
        } else {
            logger.accept("error failed to load offsets wallhack inactive");
        }
    }

    /**
     * finds and attaches to the game process
     */
    private void attachToProcess() {
        logger.accept("searching for process: " + PROCESS_NAME);
        int processId = ProcessFinder.getProcessIdByName(PROCESS_NAME);

        if (processId == 0) {
            logger.accept("process " + PROCESS_NAME + " not found");
            return;
        }
        logger.accept("process found pid: " + processId);

        processHandle = Kernel32.INSTANCE.OpenProcess(WinNT.PROCESS_VM_READ | WinNT.PROCESS_QUERY_INFORMATION, false, processId);
        if (processHandle == null) {
            logger.accept("error could not open process code: " + Native.getLastError());
            return;
        }
        logger.accept("process opened successfully");

        lorbeerKernelDriver = new LorbeerKernelDriver(processHandle);

        clientModuleBase = ProcessFinder.getModuleBaseAddress(processHandle, processId, "client.dll");
        if(clientModuleBase == 0) {
            logger.accept("error client dll not found");
            detach();
        } else {
            logger.accept("client dll base address: 0x" + Long.toHexString(clientModuleBase));
        }
    }

    /**
     * detaches from the game process and releases handle
     */
    public void detach() {
        if (processHandle != null) {
            Kernel32.INSTANCE.CloseHandle(processHandle);
            processHandle = null;
            lorbeerKernelDriver = null;
            logger.accept("process handle closed");
        }
    }

    /**
     * checks if process is attached
     * @return true if attached false otherwise
     */
    public boolean isProcessAttached() {
        return processHandle != null && clientModuleBase != 0;
    }

    /**
     * gets the memory reader instance
     * @return the memory reader
     */
    public LorbeerKernelDriver getMemoryReader() {
        return lorbeerKernelDriver;
    }

    /**
     * gets the client dll base address
     * @return the client base address
     */
    public long getClientModuleBase() {
        return clientModuleBase;
    }

    /**
     * gets the offsets instance
     * @return the offsets
     */
    public Offsets getOffsets() {
        return offsets;
    }
}

package de.laurel.lorbeerwalls.utils;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import de.laurel.lorbeerwalls.structs.Vector3;

/**
 * utility class for reading process memory
 * @author lorberry+chatgpt
 */
public class LorbeerKernelDriver {
    /** handle to the target process */
    private final WinNT.HANDLE processHandle;

    /**
     * creates a memory reader
     * @param processHandle handle to the process to read from
     */
    public LorbeerKernelDriver(WinNT.HANDLE processHandle) {
        this.processHandle = processHandle;
    }

    /**
     * reads a block of memory
     * @param address the memory address
     * @param bytesToRead number of bytes to read
     * @return a pointer to the read memory or null
     */
    public Pointer readMemory(long address, int bytesToRead) {
        if (processHandle == null) return null;
        Memory memory = new Memory(bytesToRead);
        IntByReference bytesRead = new IntByReference();
        boolean success = Kernel32.INSTANCE.ReadProcessMemory(processHandle, new Pointer(address), memory, bytesToRead, bytesRead);
        if (success && bytesRead.getValue() == bytesToRead) {
            return memory;
        } else {
            return null;
        }
    }

    /**
     * reads a 64bit integer
     * @param address the memory address
     * @return the long value or 0
     */
    public long readLong(long address) {
        Pointer ptr = readMemory(address, 8);
        return ptr != null ? ptr.getLong(0) : 0;
    }

    /**
     * reads a 32bit integer
     * @param address the memory address
     * @return the int value or 0
     */
    public int readInt(long address) {
        Pointer ptr = readMemory(address, 4);
        return ptr != null ? ptr.getInt(0) : 0;
    }

    /**
     * reads a 32bit float
     * @param address the memory address
     * @return the float value or 0
     */
    public float readFloat(long address) {
        Pointer ptr = readMemory(address, 4);
        return ptr != null ? ptr.getFloat(0) : 0;
    }

    /**
     * reads a Vector3 struct from memory
     * @param address the memory address
     * @return the Vector3 object or null
     */
    public Vector3 readVector3(long address) {
        Pointer ptr = readMemory(address, 12);
        if (ptr != null) {
            return new Vector3(ptr.getFloat(0), ptr.getFloat(4), ptr.getFloat(8));
        }
        return null;
    }
}

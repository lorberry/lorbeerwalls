package de.laurel.lorbeerwalls.utils;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Tlhelp32;
import com.sun.jna.platform.win32.WinNT;

/**
 * utility for finding windows processes and modules
 * @author lorberry+chatgpt
 */
public class ProcessFinder {

    /**
     * gets a process id by its executable name
     * @param processName the name of the process
     * @return the process id or 0 if not found
     */
    public static int getProcessIdByName(String processName) {
        Tlhelp32.PROCESSENTRY32 processEntry = new Tlhelp32.PROCESSENTRY32();
        WinNT.HANDLE snapshot = Kernel32.INSTANCE.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, new WinNT.DWORD(0));
        try {
            while (Kernel32.INSTANCE.Process32Next(snapshot, processEntry)) {
                if (Native.toString(processEntry.szExeFile).equals(processName)) {
                    return processEntry.th32ProcessID.intValue();
                }
            }
        } finally {
            Kernel32.INSTANCE.CloseHandle(snapshot);
        }
        return 0;
    }

    /**
     * gets a module's base address within a process
     * @param processHandle handle to the target process
     * @param processId id of the target process
     * @param moduleName name of the module
     * @return the base address or 0 if not found
     */
    public static long getModuleBaseAddress(WinNT.HANDLE processHandle, int processId, String moduleName) {
        Tlhelp32.MODULEENTRY32W moduleEntry = new Tlhelp32.MODULEENTRY32W();
        WinNT.HANDLE snapshot = Kernel32.INSTANCE.CreateToolhelp32Snapshot(
                Tlhelp32.TH32CS_SNAPMODULE, new WinNT.DWORD(processId));
        try {
            while(Kernel32.INSTANCE.Module32NextW(snapshot, moduleEntry)) {
                if(moduleEntry.szModule().equals(moduleName)) {
                    return Pointer.nativeValue(moduleEntry.modBaseAddr);
                }
            }
        } finally {
            Kernel32.INSTANCE.CloseHandle(snapshot);
        }
        return 0;
    }
}

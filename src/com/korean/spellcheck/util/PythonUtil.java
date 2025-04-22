package com.korean.spellcheck.util;

import static com.korean.spellcheck.log.Log.logInfo;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

public class PythonUtil {
    private static final Shell shell = new Shell();
    private static final String PACKAGE_REQUESTS = "requests";
    private static final String PACKAGE_SETUPTOOLS = "setuptools";

    private static Boolean pythonInstalledCache = null;
    private static final Map<String, Boolean> packageInstalledCache = new HashMap<>();

    public static Boolean checkPythonUtil() {
        try {
        	logInfo(packageInstalledCache.toString());
            // Python 설치 여부 확인
            if (!isPythonInstalled()) {
                logInfo("Python is not installed.");
                openPythonDownloadPage(); // 파이썬 다운로드 페이지 열기
                return false;
            }

            // setupTools 패키지 설치 여부 확인
            if (!isPackageInstalled(PACKAGE_SETUPTOOLS)) {
                if (!confirmPackageInstallation(PACKAGE_SETUPTOOLS)) {
                    logInfo("User chose not to install the setupTools package.");
                    return false;
                }
                installPackage(PACKAGE_SETUPTOOLS);
            }

            // requests 패키지 설치 여부 확인
            if (!isPackageInstalled(PACKAGE_REQUESTS)) {
                if (!confirmPackageInstallation(PACKAGE_REQUESTS)) {
                    logInfo("User chose not to install the requests package.");
                    return false;
                }
                installPackage(PACKAGE_REQUESTS);
            }

            return true;
        } catch (IOException | InterruptedException e) {
            logInfo("Error checking Python installation: " + e.getMessage());
            return false;
        }
    }

    private static boolean isPythonInstalled() throws IOException, InterruptedException {
        // 캐시된 값이 있으면 반환
        if (pythonInstalledCache != null) {
            return pythonInstalledCache;
        }

        ProcessBuilder checkBuilder = new ProcessBuilder("python", "--version");
        logInfo("pythonBuilder");
        Process checkProcess = checkBuilder.start();
        int checkExitCode = checkProcess.waitFor();

        try (BufferedReader checkReader = new BufferedReader(new InputStreamReader(checkProcess.getInputStream(), StandardCharsets.UTF_8))) {
            String checkLine = checkReader.readLine();
            pythonInstalledCache = (checkExitCode == 0 && checkLine != null);
        }

        logInfo("Python is " + (pythonInstalledCache ? "installed" : "not installed"));
        return pythonInstalledCache;
    }

    private static boolean isPackageInstalled(String packageName) throws IOException, InterruptedException {
        // 캐시된 값이 있으면 반환
        if (packageInstalledCache.containsKey(packageName)) {
            return packageInstalledCache.get(packageName);
        }
        
        logInfo("installBuilder");

        ProcessBuilder checkBuilder = new ProcessBuilder("python", "-m", "pip", "show", packageName);
        Process checkProcess = checkBuilder.start();
        int checkExitCode = checkProcess.waitFor();

        boolean isInstalled;
        try (BufferedReader checkReader = new BufferedReader(new InputStreamReader(checkProcess.getInputStream(), StandardCharsets.UTF_8))) {
            isInstalled = (checkExitCode == 0 && checkReader.readLine() != null);
        }

        packageInstalledCache.put(packageName, isInstalled);
        logInfo(packageName + " package is " + (isInstalled ? "installed" : "not installed"));
        return isInstalled;
    }

    private static void installPackage(String packageName) throws IOException, InterruptedException {
        ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(shell);
        
        try {
            progressDialog.run(true, false, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InterruptedException {
                    monitor.beginTask("Downloading " + packageName + " package...", IProgressMonitor.UNKNOWN);
                    
                    try {
                        ProcessBuilder pipInstallBuilder = new ProcessBuilder("python", "-m", "pip", "install", packageName);
                        Process pipProcess = pipInstallBuilder.start();
                        int pipExitCode = pipProcess.waitFor();

                        try (BufferedReader pipErrorReader = new BufferedReader(new InputStreamReader(pipProcess.getErrorStream(), StandardCharsets.UTF_8))) {
                            String pipErrorLine;
                            while ((pipErrorLine = pipErrorReader.readLine()) != null) {
                                logInfo("Pip Error: " + pipErrorLine);
                            }
                        }

                        if (pipExitCode == 0) {
                            logInfo(packageName + " package installed successfully.");
                            packageInstalledCache.put(packageName, true); // 설치 성공 시 캐시 업데이트
                        } else {
                            logInfo("Failed to install " + packageName + " package.");
                        }
                    } catch (IOException e) {
                        logInfo("Error installing " + packageName + " package: " + e.getMessage());
                    } finally {
                        monitor.done();
                    }
                }
            });
        } catch (Exception e) {
            logInfo("Error showing progress dialog: " + e.getMessage());
        }
    }

    private static boolean confirmPackageInstallation(String packageName) {
        return MessageDialog.openConfirm(shell, "Install " + packageName + " Package",
            "파이썬 " + packageName + " 패키지 설치가 필요합니다. 설치하시겠습니까?\n" +
            "(The " + packageName + " package is not installed. Do you want to install it?)");
    }


    private static void openPythonDownloadPage() {
        if (!MessageDialog.openConfirm(shell, "파이썬이 설치되어있지 않습니다.", "파이썬 다운로드 페이지로 이동하시겠습니까?\n" + 
        	"(Would you like to go to the Python download page?)")) {
            return;
        }

        try {
            Desktop desktop = Desktop.getDesktop();
            URI uri = new URI("https://www.python.org/downloads/");
            desktop.browse(uri);
        } catch (IOException | URISyntaxException e) {
            logInfo("Error opening Python download page: " + e.getMessage());
        }
    }
}

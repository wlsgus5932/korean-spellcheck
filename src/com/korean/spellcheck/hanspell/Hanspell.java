package com.korean.spellcheck.hanspell;

import static com.korean.spellcheck.log.Log.logInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import com.korean.spellcheck.Activator;

public class Hanspell {
    public String spellCheck(String text) {
        try {
        	Bundle bundle = FrameworkUtil.getBundle(Activator.class);
            URL fileUrl = FileLocator.toFileURL(bundle.getEntry("/python/"));

            // 파일 경로로 변환
            File dir = new File(fileUrl.getFile());
            String pythonPath = dir.getAbsolutePath() + "/py-hanspell/spellcheck.py";

            return executePythonScript(pythonPath, text);
        } catch (Exception e) {
            logInfo("Error during Python execution: " + e.getMessage());
        }

        return null;
    }

    private String executePythonScript(String pythonPath, String text) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("python", pythonPath, text);
        Process process = processBuilder.start();

        // 에러 로그 출력
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "euc-kr"));
        String errorLine;
        while ((errorLine = errorReader.readLine()) != null) {
            logInfo("Error: " + errorLine);
        }

        // 결과 읽기
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "euc-kr"));
        StringBuilder result = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            result.append(line).append("\n");
        }
        
        return result.toString().trim(); // Python 결과를 반환
    }
    
}

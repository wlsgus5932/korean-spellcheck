package com.korean.spellcheck.editor;

import org.eclipse.core.resources.IFile;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;

public class MarkerManager {

    private static final String MARKER_TYPE = IMarker.PROBLEM; // Eclipse 기본 문제 마커 타입

    public static void addWarningMarker(IFile file, String message, int lineNumber, int charStart, int charEnd, String correctedText) {
        removeMarkersAtLine(file, lineNumber);
        
        try {
            IMarker marker = file.createMarker(MARKER_TYPE);
            marker.setAttribute(IMarker.MESSAGE, message);
            marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
            marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
            marker.setAttribute(IMarker.CHAR_START, charStart);
            marker.setAttribute(IMarker.CHAR_END, charEnd);
            marker.setAttribute("correctedText", correctedText); // 변경된 텍스트 저장
            marker.setAttribute("isSpellcheckMarker", true);
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    public static void clearMarkers(IFile file) {
        try {
            file.deleteMarkers(MARKER_TYPE, true, IFile.DEPTH_INFINITE);
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }
    
    public static void removeMarkersAtLine(IFile file, int lineNumber) {
        try {
            IMarker[] markers = file.findMarkers(null, false, IResource.DEPTH_INFINITE); // 모든 마커를 검색

            if (markers.length == 0) {
                return;
            }

            for (IMarker marker : markers) {
                int markerLineNumber = marker.getAttribute(IMarker.LINE_NUMBER, -1);
                if (markerLineNumber == lineNumber) {
                    marker.delete();
                }
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }
}

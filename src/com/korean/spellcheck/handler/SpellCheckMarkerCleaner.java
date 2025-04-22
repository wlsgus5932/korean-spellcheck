package com.korean.spellcheck.handler;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;

public class SpellCheckMarkerCleaner implements IDocumentListener {

    private final IDocument document;
    private final IFile file;

    public SpellCheckMarkerCleaner(IDocument document, IFile file) {
        this.document = document;
        this.file = file;
    }

    @Override
    public void documentChanged(DocumentEvent event) {
        try {
            // 수정된 범위
            int start = event.getOffset();
            int end = start + event.getLength();

            // 파일에서 모든 마커 가져오기
            IMarker[] markers = file.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO);
            for (IMarker marker : markers) {
                if (!marker.exists()) continue;

                // spellcheck가 만든 마커인지 확인 (isSpellcheckMarker == true)
                if (!Boolean.TRUE.equals(marker.getAttribute("isSpellcheckMarker"))) continue;

                int markerStart = marker.getAttribute(IMarker.CHAR_START, -1);
                int markerEnd = marker.getAttribute(IMarker.CHAR_END, -1);
                
                // 수정된 범위와 마커 범위가 겹치는지 확인
                if (start >= markerEnd || end <= markerStart) {
                    continue;  // 수정된 범위와 마커 범위가 겹치지 않으면 skip
                }

                // 현재 수정된 텍스트
                String currentText = document.get(markerStart, markerEnd - markerStart);
                String correctedText = marker.getAttribute("correctedText", "");

                // 수정된 텍스트와 기존 텍스트가 다르면 해당 마커 삭제
                if (!currentText.equals(correctedText)) {
                    marker.delete();  // 수정된 부분이 다르면 마커 삭제
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void documentAboutToBeChanged(DocumentEvent event) {
    }
}


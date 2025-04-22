package com.korean.spellcheck.editor;

import static com.korean.spellcheck.log.Log.logInfo;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.eclipse.core.resources.IMarker;

public class QuickFixResolutionGenerator implements IMarkerResolutionGenerator2 {

    @Override
    public IMarkerResolution[] getResolutions(IMarker marker) {
        try {
            // 마커에서 correctedText 속성 가져오기
            String correctedText = (String) marker.getAttribute("correctedText");
            if (correctedText != null) {
                return new IMarkerResolution[] {
                    new QuickFix("맞춤법 수정: " + correctedText)
                };
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new IMarkerResolution[0];
    }

    @Override
    public boolean hasResolutions(IMarker marker) {
        try {
            // 특정 마커 타입에 대해서만 Quick Fix 제공 (IMarker.PROBLEM)
        	logInfo(marker.getType());
            return marker.getType().equals(IMarker.PROBLEM);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

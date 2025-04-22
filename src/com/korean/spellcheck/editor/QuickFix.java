package com.korean.spellcheck.editor;

import static com.korean.spellcheck.log.Log.logInfo;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

public class QuickFix implements IMarkerResolution {

    private String label;

    public QuickFix(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label; // Quick Fix 버튼에 표시될 텍스트
    }

    @Override
    public void run(IMarker marker) {
        try {
            logInfo("Quick Fix 실행");

            String correctedText = (String) marker.getAttribute("correctedText");
            int charStart = (Integer) marker.getAttribute(IMarker.CHAR_START);
            int charEnd = (Integer) marker.getAttribute(IMarker.CHAR_END);
            int originalLength = charEnd - charStart;
            int newLength = correctedText.length();
            int shiftAmount = newLength - originalLength;

            Display.getDefault().asyncExec(() -> {
                IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                if (window == null) {
                    System.out.println("Error: WorkbenchWindow is null");
                    return;
                }

                IWorkbenchPage page = window.getActivePage();
                if (page == null) {
                    System.out.println("Error: ActivePage is null");
                    return;
                }

                IEditorPart editorPart = page.getActiveEditor();
                if (editorPart == null) {
                    System.out.println("Error: ActiveEditor is null");
                    return;
                }

                ITextEditor textEditor = null;
                if (editorPart instanceof ITextEditor) {
                    textEditor = (ITextEditor) editorPart;
                } else {
                    textEditor = editorPart.getAdapter(ITextEditor.class);
                }

                if (textEditor == null) {
                    System.out.println("Error: Could not retrieve ITextEditor");
                    return;
                }

                IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
                try {
					document.replace(charStart, originalLength, correctedText);
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

                IMarker[] markers;
				try {
					markers = marker.getResource().findMarkers(null, true, 0);
					 for (IMarker otherMarker : markers) {
		                    if (otherMarker.equals(marker)) {
		                        continue;
		                    }

		                    int otherStart = (Integer) otherMarker.getAttribute(IMarker.CHAR_START);
		                    int otherEnd = (Integer) otherMarker.getAttribute(IMarker.CHAR_END);

		                    if (otherStart > charStart) {
		                        otherMarker.setAttribute(IMarker.CHAR_START, otherStart + shiftAmount);
		                        otherMarker.setAttribute(IMarker.CHAR_END, otherEnd + shiftAmount);
		                    }
		                }

		                marker.delete();
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

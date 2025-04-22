package com.korean.spellcheck.handler;

import static com.korean.spellcheck.log.Log.logInfo;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import com.korean.spellcheck.hanspell.Hanspell;
import com.korean.spellcheck.editor.MarkerManager;
import com.korean.spellcheck.util.PythonUtil;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpellCheckHandler extends AbstractHandler {
    private static final Pattern KOREAN_PATTERN = Pattern.compile("[ㄱ-ㅎㅏ-ㅣ가-힣]+([\\s\\S]*[ㄱ-ㅎㅏ-ㅣ가-힣]+)*");

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if (!PythonUtil.checkPythonUtil()) {
            logInfo("PythonUtil이 유효하지 않습니다. 맞춤법 검사를 실행할 수 없습니다.");
            return null;
        }

        try {
            IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

            if (editor instanceof AbstractTextEditor) {
                AbstractTextEditor textEditor = (AbstractTextEditor) editor;

                // ITextSelection 가져오기
                ITextSelection selection = (ITextSelection) textEditor.getSelectionProvider().getSelection();
                String selectedText = selection.getText();

                // 선택된 텍스트가 없을 경우 알림 띄우기
                if (selectedText.isEmpty()) {
                    logInfo("선택된 텍스트가 없습니다.");
                    // 알림 창 띄우기
                    MessageDialog.openInformation(
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                        "맞춤법 검사",
                        "검사할 텍스트를 선택해 주세요."
                    );
                    return null;
                }

                // 선택된 텍스트에 한국어가 포함되지 않은 경우 알림 띄우기
                Matcher koreanMatcher = KOREAN_PATTERN.matcher(selectedText);
                if (!koreanMatcher.find()) {
                    logInfo("선택된 텍스트에 한국어가 없습니다.");
                    // 알림 창 띄우기
                    MessageDialog.openInformation(
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                        "맞춤법 검사",
                        "선택된 텍스트에 한국어가 포함되어 있지 않습니다."
                    );
                    return null;
                }

                // 에디터 입력을 IFile로 변환
                IFile file = Adapters.adapt(textEditor.getEditorInput(), IFile.class);
                if (file == null) {
                    logInfo("파일 정보를 가져올 수 없습니다.");
                    return null;
                }

                Matcher matcher = KOREAN_PATTERN.matcher(selectedText);

                while (matcher.find()) {
                    String koreanText = matcher.group();

                    try {
                        Hanspell hanspell = new Hanspell();
                        String correctedText = hanspell.spellCheck(koreanText);

                        if (!koreanText.equals(correctedText)) {
                            // 선택된 텍스트의 시작 및 끝 위치 계산
                            int charStart = selection.getOffset() + matcher.start();
                            int charEnd = selection.getOffset() + matcher.end();

                            // 마커 추가
                            MarkerManager.addWarningMarker(
                                file,
                                String.format("'%s' 부분에 맞춤법 오류가 있을 수 있습니다.%s변경 -> '%s'",
                                        koreanText, System.lineSeparator(), correctedText),
                                selection.getStartLine(),
                                charStart,
                                charEnd,
                                correctedText
                            );

                            // 문서 리스너 추가: 마커를 검사할 리스너 등록
                            SpellCheckMarkerCleaner cleaner = new SpellCheckMarkerCleaner(textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput()), file);
                            textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput()).addDocumentListener(cleaner);
                        } else {
                            // 선택된 라인의 기존 마커 제거
                            MarkerManager.removeMarkersAtLine(file, selection.getStartLine());
                        }
                    } catch (Exception e) {
                        logInfo("맞춤법 검사 중 오류 발생: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            logInfo("맞춤법 검사 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}

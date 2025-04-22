import sys
from hanspell import spell_checker

# 명령줄 인자로 입력된 문자열 가져오기
if len(sys.argv) > 1:
    text = sys.argv[1]
    hanspell_sent = spell_checker.check(text)
    print(hanspell_sent.checked)
else:
    print("No input text provided.")
chạy console test
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter *.java com).FullName
[Console]::OutputEncoding = [Console]::InputEncoding = [System.Text.Encoding]::UTF8
java "-Dfile.encoding=UTF-8" "-Dstdout.encoding=UTF-8" "-Dstdin.encoding=UTF-8" -cp out com.smartphonestore.Main
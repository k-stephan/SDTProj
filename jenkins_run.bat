rem set EEip=11.16.10.163
cd %WORKSPACE%
curl http://%EEip%:8099/repo/SDT/master > repo_version.file
set /P repo_jar=<repo_version.file
if not exist %repo_jar% curl -o %repo_jar% http://%EEip%:8099/download/getSDTRepo/master
java -cp %repo_jar% sdt.utils.RunFeature

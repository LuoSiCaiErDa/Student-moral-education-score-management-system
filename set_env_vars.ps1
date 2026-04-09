[Environment]::SetEnvironmentVariable('JAVA_HOME','C:\Program Files\Microsoft\jdk-17.0.18.8-hotspot','User')
[Environment]::SetEnvironmentVariable('MAVEN_HOME','D:\Student-moral-education-score-management-system\apache-maven-3.9.9','User')
$currentPath = [Environment]::GetEnvironmentVariable('PATH','User')
$newPath = $currentPath + ';C:\Program Files\Microsoft\jdk-17.0.18.8-hotspot\bin;D:\Student-moral-education-score-management-system\apache-maven-3.9.9\bin'
[Environment]::SetEnvironmentVariable('PATH',$newPath,'User')
Write-Host 'Environment variables saved.'

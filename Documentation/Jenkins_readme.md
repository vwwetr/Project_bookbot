## Jenkins CLI
Примеры:
Информация справочная для  Jenkins API

http://<jenkins host>/api/

Получить информацию в формате json

http://<jenkins host>/api/json?pretty=true

Сделать фильтры по name, color, url

http://<host>/api/json?pretty=true&tree=jobs[name,color, url]

Получить информацию о конкретной сборке

http://<host>/job/<JOB_NAME>/api/json?pretty=true

 curl -X POST "http://jenkins-main.example.ru:8080/job/test/build" --user admin:1144997d7ca94721008ddb1a4b62514ed8
 curl -X POST "http://jenkins-main.example.ru:8080/job/param/buildWithParameters?NAME=Alex" --user admin:1144997d7ca94721008ddb1a4b62514ed8
 curl -X POST "http://jenkins-main.example.ru:8080/job/exampleTwo/Project001/lastBuild/api/json" --user admin:1144997d7ca94721008ddb1a4b62514ed8
 curl -X POST "http://jenkins-main.example.ru:8080/job/param/lastBuild/console" --user admin:1144997d7ca94721008ddb1a4b62514ed8

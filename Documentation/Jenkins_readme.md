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

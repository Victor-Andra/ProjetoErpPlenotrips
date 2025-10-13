projeto_api_plenotrips
{
Após clonar o projeto abra ele no seu vscode e atravez do terminal atualize os pacotes utilizados a seguir.

Executar os comandos respectivos de go get:

//dependencias jwt para gerar e validar o token
go get github.com/golang-jwt/jwt/v5
go get github.com/dgrijalva/jwt-go

//dependencia do pgadmin para coneção com o banco
go get github.com/lib/pq

//framework para criar api rest com facilidade simplificaando requisições
go get github.com/gin-gonic/gin

//dependencia para utilizar uuid nos registros do banco
go get github.com/google/uuid

//dependencias do swagger
go get -u github.com/swaggo/swag/cmd/swag
go get -u github.com/swaggo/gin-swagger
go get -u github.com/swaggo/gin-swagger/swaggerFiles

Uma vez que todas dependências estiverem importadas podemos executar o arquivo docker.

docker-compose up --build
Se tudo der certo aparecerá no log 
"✅ Conexão com banco estabelecida com sucesso!"
}

projeto_plenotrips
{
Após clonar o projeto importe ele no seu editor de preferência como um projeto maven.
Uma vez que ele for importado clique no botão direito na pasta diretamente, vá até maven e execute "project update".
Quando ele for atualizado basta executar como uma aplicação java o arquivo ProjetoPlenotripsApplication.java no caminho /projeto_plenotrips/src/main/java/com/plenotrip/ProjetoPlenotripsApplication.java
}


*** Settings ***
Library           Collections
Library           RequestsLibrary
Test Timeout      30 seconds

*** Test Cases ***

addActorPass
    Create Session    localhost    http://localhost:8080
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    name=Kevin Bacon    actorId=nm0000102
    ${resp}=    Put Request    localhost    /api/v1/addActor    data=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    200

addActorFail
    Create Session    localhost    http://localhost:8080
    ${headers}=    Create Dictionary   Content-Type=application/json
    ${params}=    Create Dictionary    name=Kevin Bacon
    ${resp}=    Put Request    localhost    /api/v1/addActor   data=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    400

addMoviePass
    Create Session    localhost    http://localhost:8080
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    name=Parasite    movieId=nm7001453
    ${resp}=    Put Request    localhost    /api/v1/addMovie    data=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    200

addMovieFail
    Create Session    localhost    http://localhost:8080
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    movieId=nm7001453
    ${resp}=    Put Request    localhost    /api/v1/addMovie    data=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    400

addRelationshipPass
    Create Session    localhost    http://localhost:8080
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    movieId=nm7001453    actorId=nm0000102
    ${resp}=    Put Request    localhost    /api/v1/addRelationship    data=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    200

addRelationshipFail
    Create Session    localhost    http://localhost:8080
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=nm0000102
    ${resp}=    Put Request    localhost    /api/v1/addRelationship    data=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    400

addRelationshipFail
    Create Session    localhost    http://localhost:8080
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=nm0000102    movieId=nm70353 
    ${resp}=    Put Request    localhost    /api/v1/addRelationship    data=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    404

getActorPass
    Create Session    localhost    http://localhost:8080
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=nm0000102
    ${resp}=    Get Request    localhost    /api/v1/getActor    json=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    200

getActorFail
    Create Session    localhost    http://localhost:8080
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=nm0000
    ${resp}=    Get Request    localhost    /api/v1/getActor    json=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    404

getMoviePass
    Create Session    localhost    http://localhost:8080
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    movieId=nm7001453
    ${resp}=    Get Request    localhost    /api/v1/getMovie    json=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    200

getMovieFail
    Create Session    localhost    http://localhost:8080
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    movieId=nm700145
    ${resp}=    Get Request    localhost    /api/v1/getMovie    json=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    404
hasRelationshipPass
    Create Session    localhost    http://localhost:8080
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    movieId=nm7001453    actorId=nm0000102
    ${resp}=    Get Request    localhost    /api/v1/hasRelationship    json=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    200

hasRelationshipFail
    Create Session    localhost    http://localhost:8080
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    movieId=nm70013    actorId=nm0000102
    ${resp}=    Get Request    localhost    /api/v1/hasRelationship    json=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    404

computeBaconNumberPass
    Create Session    localhost    http://localhost:8080
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=nm0000102
    ${resp}=    Get Request    localhost    /api/v1/computeBaconNumber    json=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    200

computeBaconNumberFail
    Create Session    localhost    http://localhost:8080
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=nm102
    ${resp}=    Get Request    localhost    /api/v1/computeBaconNumber    json=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    404

computeBaconPathPass
    Create Session    localhost    http://localhost:8080
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=nm0000102
    ${resp}=    Get Request    localhost    /api/v1/computeBaconPath    json=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    200

computeBaconPathFail
    Create Session    localhost    http://localhost:8080
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=nm102
    ${resp}=    Get Request    localhost    /api/v1/computeBaconPath    json=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    404

getCollaboratorsPass
    Create Session    localhost    http://localhost:8080
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=nm0000102    howMany=2
    ${resp}=    Get Request    localhost    /api/v1/getCollaborators    json=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    200

getCollaboratorsFail
    Create Session    localhost    http://localhost:8080
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=nm102
    ${resp}=    Get Request    localhost    /api/v1/getCollaborators    json=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    404

getPathToSigOthersPass
    Create Session    localhost    http://localhost:8080
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=nm0000102    SO_ID=nk12345
    ${resp}=    Get Request    localhost    /api/v1/getPathToSigOthers    json=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    200

getPathToSigOthersFail
    Create Session    localhost    http://localhost:8080
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    actorId=nm0000102    SO_ID=2
    ${resp}=    Get Request    localhost    /api/v1/getPathToSigOthers    json=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    404

getPopularActorListPass
    Create Session    localhost    http://localhost:8080
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    numberOfActors=1
    ${resp}=    Get Request    localhost    /api/v1/getPopularActorList    json=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    200

getPopularActorListFail
    Create Session    localhost    http://localhost:8080
    ${headers}=    Create Dictionary    Content-Type=application/json
    ${params}=    Create Dictionary    numberOfActors=-1
    ${resp}=    Get Request    localhost    /api/v1/getPopularActorList    json=${params}    headers=${headers}
    Should Be Equal As Strings    ${resp.status_code}    400

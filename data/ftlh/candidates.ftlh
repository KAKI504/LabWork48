<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Vote for us!</title>
  <link rel="stylesheet" href="/css/candidates.css">
</head>
<body>
  <div class="container flex flex-col align-center">
    <h1>Vote for us</h1>

    <main class="flex flex-wrap align-evenly">
      <#list candidates as candidate>
        <div class="card">
          <form action="/vote" method="post" class="flex flex-col align-center">
            <img src="/data/img/${candidate.photo}" alt="${candidate.name}">
            <p>${candidate.name}</p>
            <input name="candidateId" type="hidden" value="${candidate.id}">
            <button type="submit" class="vote">Vote</button>
          </form>
        </div>
      </#list>
    </main>

    <div class="separator"></div>

    <a class="back flex align-center" href="/votes">Show total votes</a>
  </div>
</body>
</html>
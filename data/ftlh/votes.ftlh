<!DOCTYPE html>
<html lang="en">
<head>
   <meta charset="UTF-8">
   <meta name="viewport" content="width=device-width, initial-scale=1.0">
   <title>Who's winning?</title>
   <link rel="stylesheet" href="/css/candidates.css">
</head>
<body>
   <div class="container flex flex-col align-center">
     <h1>VOTING RESULTS</h1>

     <div class="results-info">
       <p>Total votes: <span class="highlight">${totalVotes}</span></p>
     </div>

     <main class="flex flex-wrap align-evenly">
       <#list candidates as candidate>
         <div class="card <#if candidate?index == 0 && candidate.votes gt 0>winner</#if>">
           <div class="flex flex-col align-center">
             <div class="img-container">
               <img src="/data/img/${candidate.photo}" alt="${candidate.name}">
               <#if candidate?index == 0 && candidate.votes gt 0>
                 <div class="crown"></div>
               </#if>
             </div>
             <p>${candidate.name}</p>
             <div class="stats-container">
               <div class="progress-bar">
                 <div class="progress" style="width: ${candidate.getVotesPercentage(totalVotes)}%"></div>
               </div>
               <span class="votes-percentage">${candidate.getVotesPercentage(totalVotes)}%</span>
               <span class="votes-count">${candidate.votes} votes</span>
             </div>
           </div>
         </div>
       </#list>
     </main>

     <a class="back flex align-center" href="/">
       <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
         <line x1="19" y1="12" x2="5" y2="12"></line>
         <polyline points="12 19 5 12 12 5"></polyline>
       </svg>
       <span>Back to main</span>
     </a>
   </div>
</body>
</html>
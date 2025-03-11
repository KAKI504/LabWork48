<!DOCTYPE html>
<html lang="en">
<head>
   <meta charset="UTF-8">
   <meta name="viewport" content="width=device-width, initial-scale=1.0">
   <title>Thank you!!</title>
   <link rel="stylesheet" href="/css/candidates.css">
</head>
<body>
   <div class="container flex flex-col align-center">
     <div class="thank-you-container">
       <h1>THANK YOU FOR YOUR VOTE!</h1>

       <div class="img-container">
         <img src="/data/img/${candidate.photo}" alt="${candidate.name}">
       </div>

       <p>You voted for: <strong>${candidate.name}</strong></p>
       <p>Your candidate has <strong>${votesPercentage}%</strong> of votes.</p>
     </div>

     <a class="back flex align-center" href="/reset">
       <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
         <line x1="19" y1="12" x2="5" y2="12"></line>
         <polyline points="12 19 5 12 12 5"></polyline>
       </svg>
       <span>Reset vote</span>
     </a>
   </div>
</body>
</html>
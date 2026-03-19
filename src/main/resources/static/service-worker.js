const CACHE_NAME = 'smart-baghichaa-v1';
const CACHED_URLS = ['/', '/index.html'];

// Install: cache core pages
self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME).then(cache => cache.addAll(CACHED_URLS))
  );
  self.skipWaiting();
});

// Activate: clean old caches
self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(keys =>
      Promise.all(keys.filter(k => k !== CACHE_NAME).map(k => caches.delete(k)))
    )
  );
  self.clients.claim();
});

// Fetch: network-first with offline fallback
self.addEventListener('fetch', event => {
  if (event.request.method !== 'GET') return;

  event.respondWith(
    fetch(event.request)
      .then(response => {
        // Cache successful responses for core pages
        if (response.ok && CACHED_URLS.includes(new URL(event.request.url).pathname)) {
          const clone = response.clone();
          caches.open(CACHE_NAME).then(c => c.put(event.request, clone));
        }
        return response;
      })
      .catch(() =>
        caches.match(event.request).then(cached => {
          if (cached) return cached;
          // Offline fallback page
          return new Response(
            `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Smart Baghichaa – Offline</title>
  <style>
    body{font-family:'Outfit',sans-serif;background:#f0f4f0;display:flex;align-items:center;
         justify-content:center;min-height:100vh;margin:0;flex-direction:column;text-align:center;padding:20px;}
    h1{color:#1b3a2d;font-size:2rem;margin-bottom:12px;}
    p{color:#4f7263;font-size:1rem;max-width:400px;line-height:1.6;}
    .emoji{font-size:4rem;margin-bottom:20px;}
    button{margin-top:24px;background:#1b3a2d;color:#f6f2eb;padding:12px 28px;border:none;
           border-radius:50px;font-size:1rem;cursor:pointer;font-family:inherit;}
    button:hover{background:#3a7d55;}
  </style>
</head>
<body>
  <div class="emoji">🌿</div>
  <h1>You're Offline</h1>
  <p>Smart Baghichaa needs an internet connection to load. Please check your connection and try again.</p>
  <button onclick="location.reload()">Try Again</button>
</body>
</html>`,
            { headers: { 'Content-Type': 'text/html' } }
          );
        })
      )
  );
});

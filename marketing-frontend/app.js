import express from 'express';
import session from 'express-session';
import bodyParser from 'body-parser';
import fetch from 'node-fetch';
import Keycloak from 'keycloak-js';

const app = express();

// Middleware
app.use(bodyParser.json());
app.use(
  session({
    secret: 'keycloak-js-secret',
    resave: false,
    saveUninitialized: true,
  })
);

// Serve static files
app.use(express.static('public'));

// Keycloak configuration
const keycloakConfig = {
  url: 'https://rhbk-rhbk.apps.ocp4.example.com',
  realm: 'demo',
  clientId: 'marketing-frontend',
};

const keycloak = new Keycloak({
  url: keycloakConfig.url,
  realm: keycloakConfig.realm,
  clientId: keycloakConfig.clientId,
});

let userSession = { token: null, refreshToken: null };

// Login
app.get('/api/login', (req, res) => {
  const baseUrl = `${req.protocol}://${req.get('host')}`;
  const loginUrl =
    `${keycloakConfig.url}/realms/${keycloakConfig.realm}/protocol/openid-connect/auth` +
    `?client_id=${keycloakConfig.clientId}&response_type=code&scope=openid&redirect_uri=${baseUrl}/api/callback`;
  res.json({ loginUrl });
});

// Callback
app.get('/api/callback', async (req, res) => {
  process.env.NODE_TLS_REJECT_UNAUTHORIZED = '0';
  const code = req.query.code;
  if (!code) {
    return res.status(400).json({ error: 'Authorization code is missing' });
  }

  const baseUrl = `${req.protocol}://${req.get('host')}`;
  const tokenUrl = `${keycloakConfig.url}/realms/${keycloakConfig.realm}/protocol/openid-connect/token`;
  const body = `grant_type=authorization_code&code=${code}` +
    `&redirect_uri=${baseUrl}/api/callback&client_id=${keycloakConfig.clientId}`;

  try {
    const response = await fetch(tokenUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body,
    });
    const data = await response.json();

    if (data.access_token) {
      userSession.token = data.access_token;
      userSession.refreshToken = data.refresh_token;
      res.redirect('/');
    } else {
      res.status(400).json({ error: 'Failed to retrieve token' });
    }
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Logout
app.post('/api/logout', async (req, res) => {
  if (!userSession.token) {
    return res.status(400).json({ error: 'User is not logged in' });
  }

  const logoutUrl = `${keycloakConfig.url}/realms/${keycloakConfig.realm}/protocol/openid-connect/logout`;
  const body = `client_id=${keycloakConfig.clientId}&refresh_token=${userSession.refreshToken}`;

  try {
    await fetch(logoutUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body,
    });
    userSession = { token: null, refreshToken: null };
    res.json({ message: 'Logout successful' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Show Token
app.get('/api/show-token', (req, res) => {
  if (userSession.token) {
    res.json({ token: userSession.token });
  } else {
    res.status(401).json({ error: 'User is not logged in' });
  }
});

// List Campaigns
app.get('/api/list-campaigns', async (req, res) => {
  try {
    // Prepare headers, include Authorization if token exists
    const headers = userSession.token
      ? { Authorization: `Bearer ${userSession.token}` }
      : {};

    // Make request to campaigns API
    const response = await fetch('http://workstation.lab.example.com:3000/campaign/list', { headers });
    if (!response.ok) {
         // Return appropriate error response
         return res.status(response.status).json({
           error: `Failed to fetch campaigns. Status: ${response.status}`,
         });
       }
   
       // Parse and return campaigns
       let campaigns;
       try {
         campaigns = await response.json();
       } catch (err) {
         return res.status(500).json({ error: 'Error parsing campaigns response' });
       }
       res.json(campaigns);
     } catch (error) {
       // Log and return server error
       console.error('Error fetching campaigns:', error.message);
       res.status(500).json({ error: 'Internal Server Error', details: error.message });
     }
});
// Start server
const PORT = 8081;
app.listen(PORT, () => {
  console.log(`Server running at http://workstation.lab.example.com:${PORT}`);
});

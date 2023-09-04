const LDAPUserManagement = {
  moveToLocalRealm: async function (username) {
    const data = {};
    data.username = username;

    const resp = await fetch(webApplicationBaseURL + 'servlets/ListVanishedLDAPUsersServlet', {
      method: "POST",
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      body: JSON.stringify(username)
    }).catch(error => {
      console.error(error);
      return;
    })

    const json = await resp.json();

    if (json.status == true) {
      location.reload();
    } else {
      console.warn("Could not move user " + username + " to local realm.");
    }
  }
}


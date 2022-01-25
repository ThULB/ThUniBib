package de.uni_jena.thunibib;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.mycore.common.MCRException;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.mycore.user2.MCRRealm;
import org.mycore.user2.MCRRealmFactory;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;

@MCRCommandGroup(name = "ThUniBib Tools")

public class ThUniBibCommands {

    private static List<String> DEFAULT_ROLES = List.of("submitter","admin");

    @MCRCommand(syntax = "thunibib create editor user {0} with email {1}",
        help = "Creates a scoped shibboleth user with login {0} and {1} as mail address")
    public static void createEditorUser(String login, String mail) {
        String unscopedUserId = Objects.requireNonNull(login, "Login must not be null");
        String eMail = Objects.requireNonNull(mail, "e-mail address must not be null");
        final Optional<MCRRealm> shibbolethRealm = MCRRealmFactory.listRealms().stream()
            .filter(r -> r.getID().endsWith(".de")).findFirst();
        if (shibbolethRealm.isEmpty()) {
            throw new MCRException("Could not find configured Shibboleth realm.");
        }
        if (MCRUserManager.exists(unscopedUserId, shibbolethRealm.get())) {
            throw new MCRException("User " + unscopedUserId + "@" + shibbolethRealm.get().getID() + " already exists.");
        }
        MCRUser shibbolethUser=new MCRUser(unscopedUserId, shibbolethRealm.get());
        DEFAULT_ROLES.forEach(shibbolethUser::assignRole);
        shibbolethUser.setLocked(true); //user may not change their details
        shibbolethUser.setDisabled(true); //no direct login allowed
        shibbolethUser.setEMail(eMail);
        MCRUserManager.createUser(shibbolethUser);
    }

}

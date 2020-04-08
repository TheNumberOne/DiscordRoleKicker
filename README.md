# Discord Role Kicker Bot

## Basic Functionality
 - User is assigned a role that the bot listens to.
 - After `<X>` time, the user is warned they will be kicked if they still have the same role as before.
 - After `<Y>` more time, the user is kicked.
 
Owner Commands:
 - `.setrolekickerprefix <prefix>` - set the prefix that the bot uses for the server to what you desire.

Admin Commands:
 - `.(addrole|editrole) <role name> <X> <Y> [<warning message>]` - adds or updates a role for the bot to watch
 - `.listrole(s)` - list the roles that are currently being watched
 - `.removerole <role name>` - removes the role from the list of those that are watched
 - `.setwarningmessage <role name> <warning message>` - sets the warning messages sent to users
 
Additional requirements:
 - The bot will not ban users who can ban or with a role higher than it.
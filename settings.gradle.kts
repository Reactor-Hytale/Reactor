rootProject.name = "Reactor"

include("kernel:micro")
include("kernel:api")

include("sdk:common")
include("sdk:bundled")

include("networking:api")
include("networking:protocol")
include("networking:internal")

include("launcher:runtime")
include("launcher:minimal")
include("launcher:debug")

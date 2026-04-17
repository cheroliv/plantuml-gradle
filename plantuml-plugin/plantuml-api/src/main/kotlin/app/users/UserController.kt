@file:Suppress("unused")

package app.users


import java.util.*


data class UserController(
    val id: UUID,
    val userId: UUID,
)
/*=================================================================================*/


//@RestController
//@RequestMapping("api")
//class AccountController(
//    private val accountService: AccountService
//) {
//    internal class AccountException(message: String) : RuntimeException(message)
//
////
////    /**
////     * `GET  /authenticate` : check if the user is authenticated, and return its login.
////     *
////     * @param request the HTTP request.
////     * @return the login if the user is authenticated.
////     */
////    @GetMapping("/authenticate")
////    suspend fun isAuthenticated(request: ServerWebExchange): String? =
////        request.getPrincipal<Principal>().map(Principal::getName).awaitFirstOrNull().also {
////            d("REST request to check if the current user is authenticated")
////        }
////
////
////    /**
////     * {@code GET  /account} : get the current user.
////     *
////     * @return the current user.
////     * @throws RuntimeException {@code 500 (Internal Application Error)} if the user couldn't be returned.
////     */
////    @GetMapping("account")
////    suspend fun getAccount(): Account = i("controller getAccount").run {
////        userService.getUserWithAuthorities().run<User?, Nothing> {
////            if (this == null) throw AccountException("User could not be found")
////            else return Account(user = this)
////        }
////    }
////

//}

/*=================================================================================*/


//@RestController
//@RequestMapping("/api")
//@Suppress("unused")
//class AuthenticationController(
//    private val tokenProvider: TokenProvider,
//    private val authenticationManager: ReactiveAuthenticationManager
//) {
//    /**
//     * Object to return as body in Jwt Authentication.
//     */
//    class JwtToken(@JsonProperty(AUTHORIZATION_ID_TOKEN) val idToken: String)
//
//    @PostMapping("/authenticate")
//    suspend fun authorize(@Valid @RequestBody loginVm: Login)
//            : ResponseEntity<JwtToken> = tokenProvider.createToken(
//        authenticationManager.authenticate(
//            UsernamePasswordAuthenticationToken(
//                loginVm.username,
//                loginVm.password
//            )
//        ).awaitSingle(), loginVm.rememberMe!!
//    ).run {
//        return ResponseEntity<JwtToken>(
//            JwtToken(idToken = this),
//            HttpHeaders().apply {
//                add(
//                    AUTHORIZATION_HEADER,
//                    "$BEARER_START_WITH$this"
//                )
//            },
//            OK
//        )
//    }
//}


/*=================================================================================*/

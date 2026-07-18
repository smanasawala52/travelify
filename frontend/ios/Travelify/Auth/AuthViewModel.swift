import Foundation
import Combine

class AuthViewModel: ObservableObject {
    @Published var isLoggedIn = false
    private let authService = AuthService()
    private let keychainHelper = KeychainHelper.shared

    init() {
        checkForToken()
    }

    func checkForToken() {
        if keychainHelper.getToken() != nil {
            DispatchQueue.main.async {
                self.isLoggedIn = true
            }
        }
    }

    func login(email: String, pass: String) {
        let loginRequest = LoginRequest(email: email, pass: pass)
        authService.login(loginRequest: loginRequest) { result in
            switch result {
            case .success(let response):
                self.keychainHelper.saveToken(response.token)
                DispatchQueue.main.async {
                    self.isLoggedIn = true
                }
            case .failure(let error):
                // Handle login error
                print(error.localizedDescription)
                DispatchQueue.main.async {
                    self.isLoggedIn = false
                }
            }
        }
    }

    func logout() {
        keychainHelper.deleteToken()
        DispatchQueue.main.async {
            self.isLoggedIn = false
        }
    }
}
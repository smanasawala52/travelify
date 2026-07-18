import Foundation

struct AuthService {
    private let apiClient = APIClient.shared

    func login(loginRequest: LoginRequest, completion: @escaping (Result<LoginResponse, Error>) -> Void) {
        let url = URL(string: "login", relativeTo: apiClient.baseURL)!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.httpBody = try? JSONEncoder().encode(loginRequest)
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")

        if let token = KeychainHelper.shared.getToken() {
            request.addValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }

        apiClient.perform(request, completion: completion)
    }
}

struct LoginRequest: Codable {
    let email: String
    let pass: String
}

struct LoginResponse: Codable {
    let token: String
}
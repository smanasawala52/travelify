import Foundation

/// Shared iOS API client stub for Customer / Travel Agent / Admin targets.
enum ApiClient {
    static let defaultBaseURL = URL(string: "http://localhost:8080/api")!

    static func authHeader(token: String) -> String {
        "Bearer \(token)"
    }
}
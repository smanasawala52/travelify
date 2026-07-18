import Foundation

final class AuthStore {
    static let shared = AuthStore()
    var token: String?
    var role: String?

    func clear() {
        token = nil
        role = nil
    }
}
import SwiftUI

struct ContentView: View {
    @StateObject private var authViewModel = AuthViewModel()
    @State private var isShowingSplash = true

    var body: some View {
        ZStack {
            if isShowingSplash {
                SplashView()
            } else if authViewModel.isLoggedIn {
                ProfileView()
                    .environmentObject(authViewModel)
            } else {
                LoginView()
            }
        }
        .onAppear {
            DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                self.isShowingSplash = false
            }
        }
    }
}
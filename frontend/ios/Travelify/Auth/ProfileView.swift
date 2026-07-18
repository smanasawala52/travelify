import SwiftUI

struct ProfileView: View {
    @EnvironmentObject var authViewModel: AuthViewModel

    var body: some View {
        VStack {
            Text("Profile")
                .font(.largeTitle)
            // TODO: Add editable fields for profile data
            Button("Logout") {
                authViewModel.logout()
            }
            .padding()
        }
        .navigationTitle("Profile")
    }
}
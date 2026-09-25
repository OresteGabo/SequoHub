import UIKit
import SwiftUI
import Shared
import GoogleSignIn

final class IOSGoogleSignInProvider: IosGoogleSignInProvider {
    private let presentingViewController: () -> UIViewController?

    init(presentingViewController: @escaping () -> UIViewController?) {
        self.presentingViewController = presentingViewController
    }

    func signIn(completionHandler: @escaping (GoogleSignInResult?, Error?) -> Void) {
        guard let clientID = Bundle.main.object(forInfoDictionaryKey: "CLIENT_ID") as? String, !clientID.isEmpty else {
            completionHandler(
                GoogleSignInResultFailure(message: "Google CLIENT_ID missing in Info.plist."),
                nil
            )
            return
        }

        guard let viewController = presentingViewController() else {
            completionHandler(
                GoogleSignInResultFailure(message: "Unable to present Google sign-in."),
                nil
            )
            return
        }

        GIDSignIn.sharedInstance.configuration = GIDConfiguration(clientID: clientID)
        GIDSignIn.sharedInstance.signIn(withPresenting: viewController) { signInResult, error in
            if let error {
                completionHandler(
                    GoogleSignInResultFailure(message: error.localizedDescription),
                    nil
                )
                return
            }

            guard let user = signInResult?.user else {
                completionHandler(
                    GoogleSignInResultFailure(message: "Google sign-in returned no user."),
                    nil
                )
                return
            }

            guard let idToken = user.idToken?.tokenString, !idToken.isEmpty else {
                completionHandler(
                    GoogleSignInResultFailure(message: "Google ID token missing. Check OAuth client configuration."),
                    nil
                )
                return
            }

            completionHandler(
                GoogleSignInResultSuccess(
                    idToken: idToken,
                    displayName: user.profile?.name,
                    email: user.profile?.email,
                    profilePictureUri: user.profile?.imageURL(withDimension: 96)?.absoluteString
                ),
                nil
            )
        }
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Self.Context) -> UIViewController {
        var viewController: UIViewController?
        let googleSignInProvider = IOSGoogleSignInProvider {
            viewController
        }
        viewController = MainViewControllerKt.MainViewController(
            googleSignInProvider: googleSignInProvider
        )
        return viewController!
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
    }
}

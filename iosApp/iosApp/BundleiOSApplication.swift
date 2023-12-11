import SwiftUI
import Firebase

@main
struct BundleiOSApplication: App {
    init() {
      FirebaseApp.configure()
    }
	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}

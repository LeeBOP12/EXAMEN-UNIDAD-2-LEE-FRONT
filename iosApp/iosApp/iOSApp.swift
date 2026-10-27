import SwiftUI
import Shared

@main
struct iOSApp: App {

    // Kotlin/Native antepone "do" a las funciones cuyo nombre empieza con
    // "init", por eso initKoinIos() se invoca aqui como doInitKoinIos().
    init() {
        MainViewControllerKt.iniciarDependencias()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

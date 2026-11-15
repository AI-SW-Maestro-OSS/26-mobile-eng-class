import CommonDomain
import Foundation

public enum CoroutineModule: DIModule {
    public static func register(in container: DIContainer) {
        let ioQueue = DispatchQueue(label: "com.example.architecture.dispatcher.io", qos: .utility, attributes: .concurrent)
        let ttiQueue = DispatchQueue(label: "com.example.architecture.dispatcher.tti", qos: .utility)
        let defaultQueue = DispatchQueue(label: "com.example.architecture.dispatcher.default", qos: .userInitiated, attributes: .concurrent)

        container.register(DispatchQueue.self, named: CoroutineQualifiers.IoDispatcher, scope: .singleton) { _ in
            ioQueue
        }
        container.register(DispatchQueue.self, named: CoroutineQualifiers.TtiDispatcher, scope: .singleton) { _ in
            ttiQueue
        }
        container.register(DispatchQueue.self, named: CoroutineQualifiers.MainDispatcher, scope: .singleton) { _ in
            DispatchQueue.main
        }
        container.register(DispatchQueue.self, named: CoroutineQualifiers.DefaultDispatcher, scope: .singleton) { _ in
            defaultQueue
        }
        container.register(ArchiCoroutineScope.self, named: CoroutineQualifiers.IoScope, scope: .singleton) { _ in
            ArchiCoroutineScope(name: "IoScope", queue: ioQueue)
        }
        container.register(ArchiCoroutineScope.self, named: CoroutineQualifiers.MainScope, scope: .singleton) { _ in
            ArchiCoroutineScope(name: "MainScope", queue: DispatchQueue.main)
        }
    }
}

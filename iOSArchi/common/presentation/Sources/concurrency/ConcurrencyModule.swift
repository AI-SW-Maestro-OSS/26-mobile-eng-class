import CommonDomain
import Foundation

public enum ConcurrencyModule: DIModule {
    public static func register(in container: DIContainer) {
        let ioQueue = DispatchQueue(label: "com.example.architecture.queue.io", qos: .utility, attributes: .concurrent)
        let ttiQueue = DispatchQueue(label: "com.example.architecture.queue.tti", qos: .utility)
        let defaultQueue = DispatchQueue(label: "com.example.architecture.queue.default", qos: .userInitiated, attributes: .concurrent)

        container.register(DispatchQueue.self, named: ConcurrencyQualifiers.ioQueue, scope: .singleton) { _ in
            ioQueue
        }
        container.register(DispatchQueue.self, named: ConcurrencyQualifiers.ttiQueue, scope: .singleton) { _ in
            ttiQueue
        }
        container.register(DispatchQueue.self, named: ConcurrencyQualifiers.mainQueue, scope: .singleton) { _ in
            DispatchQueue.main
        }
        container.register(DispatchQueue.self, named: ConcurrencyQualifiers.defaultQueue, scope: .singleton) { _ in
            defaultQueue
        }
        container.register(AsyncTaskScope.self, named: ConcurrencyQualifiers.ioTaskScope, scope: .singleton) { _ in
            AsyncTaskScope(name: "ioTaskScope", queue: ioQueue)
        }
        container.register(AsyncTaskScope.self, named: ConcurrencyQualifiers.mainTaskScope, scope: .singleton) { _ in
            AsyncTaskScope(name: "mainTaskScope", queue: DispatchQueue.main)
        }
    }
}

import Foundation

/// 싱글턴 / 화면 스코프 / 일시적 라이프사이클 및 이름 기반 Qualifier를
/// iOS(Swift)에서 타입 안전하게 구현한 경량 DI 컨테이너.
public protocol DIModule {
    static func register(in container: DIContainer)
}

public enum DIScope {
    case singleton
    case scoped
    case transient
}

public final class DIContainer: @unchecked Sendable {
    public static let shared = DIContainer()

    private struct ServiceKey: Hashable {
        let typeId: ObjectIdentifier
        let name: String?
    }

    private struct Registration {
        let scope: DIScope
        let factory: (DIContainer) -> Any
    }

    private let lock = NSRecursiveLock()
    private var registrations: [ServiceKey: Registration] = [:]
    private var singletons: [ServiceKey: Any] = [:]
    private var scopedInstances: [ServiceKey: Any] = [:]
    private let parent: DIContainer?

    public init(parent: DIContainer? = nil) {
        self.parent = parent
    }

    /// 화면 단위 자식 스코프 컨테이너를 생성한다.
    /// 같은 화면 생성 그래프 안에서 `.scoped` 객체(`TTIHelper` 등)를 공유한다.
    public func makeScope() -> DIContainer {
        DIContainer(parent: self)
    }

    public func register<T>(
        _ type: T.Type,
        named: String? = nil,
        scope: DIScope = .singleton,
        factory: @escaping (DIContainer) -> T
    ) {
        lock.lock()
        defer { lock.unlock() }
        let key = ServiceKey(typeId: ObjectIdentifier(type), name: named)
        registrations[key] = Registration(scope: scope, factory: factory)
    }

    public func resolve<T>(_ type: T.Type = T.self, named: String? = nil) -> T {
        if let value = resolveOptional(type, named: named) {
            return value
        }
        fatalError("[DIContainer] No registration found for \(type) (named: \(named ?? "nil"))")
    }

    public func resolveOptional<T>(_ type: T.Type = T.self, named: String? = nil) -> T? {
        lock.lock()
        defer { lock.unlock() }
        let key = ServiceKey(typeId: ObjectIdentifier(type), name: named)

        if let registration = registrations[key] {
            return instantiate(key: key, registration: registration, requester: self)
        }
        if let parent {
            return parent.resolveFromChild(key: key, child: self)
        }
        return nil
    }

    private func resolveFromChild<T>(key: ServiceKey, child: DIContainer) -> T? {
        lock.lock()
        defer { lock.unlock() }
        if let registration = registrations[key] {
            switch registration.scope {
            case .singleton:
                if let existing = singletons[key] as? T {
                    return existing
                }
                let created = registration.factory(self)
                singletons[key] = created
                return created as? T
            case .scoped:
                return child.instantiateScoped(key: key, registration: registration)
            case .transient:
                return registration.factory(child) as? T
            }
        }
        return parent?.resolveFromChild(key: key, child: child)
    }

    private func instantiate<T>(key: ServiceKey, registration: Registration, requester: DIContainer) -> T? {
        switch registration.scope {
        case .singleton:
            if let existing = singletons[key] as? T {
                return existing
            }
            let created = registration.factory(requester)
            singletons[key] = created
            return created as? T
        case .scoped:
            return instantiateScoped(key: key, registration: registration)
        case .transient:
            return registration.factory(requester) as? T
        }
    }

    private func instantiateScoped<T>(key: ServiceKey, registration: Registration) -> T? {
        lock.lock()
        defer { lock.unlock() }
        if let existing = scopedInstances[key] as? T {
            return existing
        }
        let created = registration.factory(self)
        scopedInstances[key] = created
        return created as? T
    }
}

@propertyWrapper
public struct Injected<T> {
    private let named: String?
    public var wrappedValue: T

    public init(named: String? = nil, container: DIContainer = .shared) {
        self.named = named
        self.wrappedValue = container.resolve(T.self, named: named)
    }
}

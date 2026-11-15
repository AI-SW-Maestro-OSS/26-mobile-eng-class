import Foundation

final class StateStreamRegistry<T: AnyObject & Sendable>: @unchecked Sendable {
    private let lock = NSLock()
    private var streams: [ObjectIdentifier: ResultStream<T>] = [:]

    init() {}

    func observe(_ type: T.Type) -> AsyncStream<T> {
        streamFor(ObjectIdentifier(type)).makeStream()
    }

    func emit(_ value: T) {
        let key = ObjectIdentifier(Swift.type(of: value))
        streamFor(key).emit(value)
    }

    private func streamFor(_ typeId: ObjectIdentifier) -> ResultStream<T> {
        lock.lock()
        defer { lock.unlock() }
        if let existing = streams[typeId] {
            return existing
        }
        let created = ResultStream<T>()
        streams[typeId] = created
        return created
    }

    private final class ResultStream<Element: Sendable>: @unchecked Sendable {
        private let lock = NSLock()
        private var replayValue: Element?
        private var continuations: [UUID: AsyncStream<Element>.Continuation] = [:]

        func makeStream() -> AsyncStream<Element> {
            AsyncStream(bufferingPolicy: .bufferingNewest(64)) { continuation in
                let id = UUID()
                self.lock.lock()
                self.continuations[id] = continuation
                let latest = self.replayValue
                self.lock.unlock()

                if let latest {
                    continuation.yield(latest)
                }

                continuation.onTermination = { [weak self] _ in
                    self?.lock.lock()
                    self?.continuations.removeValue(forKey: id)
                    self?.lock.unlock()
                }
            }
        }

        func emit(_ value: Element) {
            lock.lock()
            replayValue = value
            let active = Array(continuations.values)
            lock.unlock()

            for continuation in active {
                continuation.yield(value)
            }
        }
    }
}

import CommonDomain
import Foundation

public final class RemoteJankReport: JankReport, @unchecked Sendable {
    public init() {}

    public func onSnapshot(_ snapshot: JankSnapshot) {
        // Production telemetry hook (e.g. Firebase Performance / Datadog RUM)
    }
}

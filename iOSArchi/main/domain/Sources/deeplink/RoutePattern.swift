import Foundation

public struct RoutePattern: Sendable, Equatable {
    public enum Segment: Sendable, Equatable {
        case literal(String)
        case param(String)
    }

    public let rawPattern: String
    public let segments: [Segment]

    public init(_ rawPattern: String) {
        self.rawPattern = rawPattern
        self.segments = Self.parseSegments(rawPattern)
    }

    public var literalCount: Int {
        segments.reduce(0) { count, seg in
            if case .literal = seg { return count + 1 }
            return count
        }
    }

    public func match(_ actualPath: String) -> [String: String]? {
        let actualSegments = Self.splitPath(actualPath)
        guard actualSegments.count == segments.count else {
            return nil
        }

        var params: [String: String] = [:]
        for (patternSeg, actualSeg) in zip(segments, actualSegments) {
            switch patternSeg {
            case .literal(let expected):
                if expected != actualSeg {
                    return nil
                }
            case .param(let name):
                if actualSeg.isEmpty {
                    return nil
                }
                params[name] = actualSeg.removingPercentEncoding ?? actualSeg
            }
        }
        return params
    }

    private static func parseSegments(_ pattern: String) -> [Segment] {
        splitPath(pattern).map { token in
            if token.hasPrefix("{") && token.hasSuffix("}") && token.count > 2 {
                let start = token.index(after: token.startIndex)
                let end = token.index(before: token.endIndex)
                return .param(String(token[start..<end]))
            }
            return .literal(token)
        }
    }

    private static func splitPath(_ path: String) -> [String] {
        let trimmed = path.trimmingCharacters(in: CharacterSet(charactersIn: "/"))
        guard !trimmed.isEmpty else { return [] }
        return trimmed.split(separator: "/", omittingEmptySubsequences: false).map(String.init)
    }
}

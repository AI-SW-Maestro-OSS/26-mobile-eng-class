import Foundation

public protocol HTTPErrorType: CaseIterable, Sendable {
    var type: String { get }
    var errorMsg: String { get }
    var isHandledOnDomain: Bool { get }
}

public struct ErrorCause: Error, Equatable, Sendable {
    public let message: String?

    public init(_ message: String?) {
        self.message = message
    }
}

public struct HTTPResponseError: Error, Equatable, Sendable {
    public let status: HTTPResponseStatus
    public let rawCode: Int
    public let errorRequestUrl: String
    public let message: String?
    public let cause: ErrorCause?

    public init(
        status: HTTPResponseStatus,
        rawCode: Int,
        errorRequestUrl: String,
        message: String? = nil,
        cause: ErrorCause? = nil
    ) {
        self.status = status
        self.rawCode = rawCode
        self.errorRequestUrl = errorRequestUrl
        self.message = message
        self.cause = cause
    }

    public func print(_ errorType: (any HTTPErrorType)? = nil) -> String {
        let errorMsg = errorType.map { "\n\($0.type)" } ?? ""
        return !errorMsg.isEmpty ? "\(rawCode) \(status.message)\(errorMsg)" : status.description
    }
}

public enum HTTPResponseStatus: Equatable, Sendable, CustomStringConvertible {
    case ok
    case created
    case accepted
    case notAuthoritative
    case noContent
    case reset
    case partial
    case multChoice
    case movedPerm
    case movedTemp
    case seeOther
    case notModified
    case useProxy
    case badRequest
    case unauthorized
    case paymentRequired
    case forbidden
    case notFound
    case badMethod
    case notAcceptable
    case proxyAuth
    case clientTimeout
    case conflict
    case gone
    case lengthRequired
    case preconFailed
    case entityTooLarge
    case reqTooLong
    case unsupportedType
    case reqTooMany
    case internalError
    case notImplemented
    case badGateway
    case unavailable
    case gatewayTimeout
    case version
    case unknownError
    case unknown

    public var code: Int {
        switch self {
        case .ok: return 200
        case .created: return 201
        case .accepted: return 202
        case .notAuthoritative: return 203
        case .noContent: return 204
        case .reset: return 205
        case .partial: return 206
        case .multChoice: return 300
        case .movedPerm: return 301
        case .movedTemp: return 302
        case .seeOther: return 303
        case .notModified: return 304
        case .useProxy: return 305
        case .badRequest: return 400
        case .unauthorized: return 401
        case .paymentRequired: return 402
        case .forbidden: return 403
        case .notFound: return 404
        case .badMethod: return 405
        case .notAcceptable: return 406
        case .proxyAuth: return 407
        case .clientTimeout: return 408
        case .conflict: return 409
        case .gone: return 410
        case .lengthRequired: return 411
        case .preconFailed: return 412
        case .entityTooLarge: return 413
        case .reqTooLong: return 414
        case .unsupportedType: return 415
        case .reqTooMany: return 429
        case .internalError: return 500
        case .notImplemented: return 501
        case .badGateway: return 502
        case .unavailable: return 503
        case .gatewayTimeout: return 504
        case .version: return 505
        case .unknownError: return 520
        case .unknown: return -1
        }
    }

    public var message: String {
        switch self {
        case .ok: return "Ok"
        case .created: return "Created"
        case .accepted: return "Accepted"
        case .notAuthoritative: return "Not Authoritative"
        case .noContent: return "No Content"
        case .reset: return "Reset"
        case .partial: return "Partial"
        case .multChoice: return "Mult Choice"
        case .movedPerm: return "Moved Perm"
        case .movedTemp: return "Moved Temp"
        case .seeOther: return "See Other"
        case .notModified: return "Not Modified"
        case .useProxy: return "Use Proxy"
        case .badRequest: return "Bad Request"
        case .unauthorized: return "Unauthorized"
        case .paymentRequired: return "Payment Required"
        case .forbidden: return "Forbidden"
        case .notFound: return "Not Found"
        case .badMethod: return "Bad Method"
        case .notAcceptable: return "Not Acceptable"
        case .proxyAuth: return "Proxy Auth"
        case .clientTimeout: return "Client Timeout"
        case .conflict: return "Conflict"
        case .gone: return "Gone"
        case .lengthRequired: return "Length Required"
        case .preconFailed: return "Precon Failed"
        case .entityTooLarge: return "Entity Too Large"
        case .reqTooLong: return "Req Too Long"
        case .unsupportedType: return "Unsupported Type"
        case .reqTooMany: return "Req Too Many"
        case .internalError: return "Internal Error"
        case .notImplemented: return "Not Implemented"
        case .badGateway: return "Bad Gateway"
        case .unavailable: return "Unavailable"
        case .gatewayTimeout: return "Gateway Timeout"
        case .version: return "Version"
        case .unknownError: return "Unknown Error"
        case .unknown: return "Unknown"
        }
    }

    public static let allStatuses: [HTTPResponseStatus] = [
        .ok, .created, .accepted, .notAuthoritative, .noContent, .reset, .partial,
        .multChoice, .movedPerm, .movedTemp, .seeOther, .notModified, .useProxy,
        .badRequest, .unauthorized, .paymentRequired, .forbidden, .notFound, .badMethod,
        .notAcceptable, .proxyAuth, .clientTimeout, .conflict, .gone, .lengthRequired,
        .preconFailed, .entityTooLarge, .reqTooLong, .unsupportedType, .reqTooMany,
        .internalError, .notImplemented, .badGateway, .unavailable, .gatewayTimeout,
        .version, .unknownError, .unknown,
    ]

    public static func create(_ code: Int) -> HTTPResponseStatus {
        allStatuses.first { $0.code == code } ?? .unknown
    }

    public var description: String {
        "\(code) \(message)"
    }
}

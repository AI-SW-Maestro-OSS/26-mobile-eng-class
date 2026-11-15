import Foundation

public protocol HttpErrorType: CaseIterable, Sendable {
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

public struct HttpResponseException: Error, Equatable, Sendable {
    public let status: HttpResponseStatus
    public let rawCode: Int
    public let errorRequestUrl: String
    public let msg: String?
    public let cause: ErrorCause?

    public init(
        status: HttpResponseStatus,
        rawCode: Int,
        errorRequestUrl: String,
        msg: String? = nil,
        cause: ErrorCause? = nil
    ) {
        self.status = status
        self.rawCode = rawCode
        self.errorRequestUrl = errorRequestUrl
        self.msg = msg
        self.cause = cause
    }

    public var message: String? { msg }

    public func print(_ errorType: (any HttpErrorType)? = nil) -> String {
        let errorMsg = errorType.map { "\n\($0.type)" } ?? ""
        return !errorMsg.isEmpty ? "\(rawCode) \(status.msg)\(errorMsg)" : status.description
    }
}

public enum HttpResponseStatus: Equatable, Sendable, CustomStringConvertible {
    case Ok
    case Created
    case Accepted
    case NotAuthoritative
    case NoContent
    case Reset
    case Partial
    case MultChoice
    case MovedPerm
    case MovedTemp
    case SeeOther
    case NotModified
    case UseProxy
    case BadRequest
    case Unauthorized
    case PaymentRequired
    case Forbidden
    case NotFound
    case BadMethod
    case NotAcceptable
    case ProxyAuth
    case ClientTimeout
    case Conflict
    case Gone
    case LengthRequired
    case PreconFailed
    case EntityTooLarge
    case ReqTooLong
    case UnsupportedType
    case ReqTooMany
    case InternalError
    case NotImplemented
    case BadGateway
    case Unavailable
    case GatewayTimeout
    case Version
    case UnknownError
    case Unknown

    public var code: Int {
        switch self {
        case .Ok: return 200
        case .Created: return 201
        case .Accepted: return 202
        case .NotAuthoritative: return 203
        case .NoContent: return 204
        case .Reset: return 205
        case .Partial: return 206
        case .MultChoice: return 300
        case .MovedPerm: return 301
        case .MovedTemp: return 302
        case .SeeOther: return 303
        case .NotModified: return 304
        case .UseProxy: return 305
        case .BadRequest: return 400
        case .Unauthorized: return 401
        case .PaymentRequired: return 402
        case .Forbidden: return 403
        case .NotFound: return 404
        case .BadMethod: return 405
        case .NotAcceptable: return 406
        case .ProxyAuth: return 407
        case .ClientTimeout: return 408
        case .Conflict: return 409
        case .Gone: return 410
        case .LengthRequired: return 411
        case .PreconFailed: return 412
        case .EntityTooLarge: return 413
        case .ReqTooLong: return 414
        case .UnsupportedType: return 415
        case .ReqTooMany: return 429
        case .InternalError: return 500
        case .NotImplemented: return 501
        case .BadGateway: return 502
        case .Unavailable: return 503
        case .GatewayTimeout: return 504
        case .Version: return 505
        case .UnknownError: return 520
        case .Unknown: return -1
        }
    }

    public var msg: String {
        switch self {
        case .Ok: return "Ok"
        case .Created: return "Created"
        case .Accepted: return "Accepted"
        case .NotAuthoritative: return "Not Authoritative"
        case .NoContent: return "No Content"
        case .Reset: return "Reset"
        case .Partial: return "Partial"
        case .MultChoice: return "Mult Choice"
        case .MovedPerm: return "Moved Perm"
        case .MovedTemp: return "Moved Temp"
        case .SeeOther: return "See Other"
        case .NotModified: return "Not Modified"
        case .UseProxy: return "Use Proxy"
        case .BadRequest: return "Bad Request"
        case .Unauthorized: return "Unauthorized"
        case .PaymentRequired: return "Payment Required"
        case .Forbidden: return "Forbidden"
        case .NotFound: return "Not Found"
        case .BadMethod: return "Bad Method"
        case .NotAcceptable: return "Not Acceptable"
        case .ProxyAuth: return "Proxy Auth"
        case .ClientTimeout: return "Client Timeout"
        case .Conflict: return "Conflict"
        case .Gone: return "Gone"
        case .LengthRequired: return "Length Required"
        case .PreconFailed: return "Precon Failed"
        case .EntityTooLarge: return "Entity Too Large"
        case .ReqTooLong: return "Req Too Long"
        case .UnsupportedType: return "Unsupported Type"
        case .ReqTooMany: return "Req Too Many"
        case .InternalError: return "Internal Error"
        case .NotImplemented: return "Not Implemented"
        case .BadGateway: return "Bad Gateway"
        case .Unavailable: return "Unavailable"
        case .GatewayTimeout: return "Gateway Timeout"
        case .Version: return "Version"
        case .UnknownError: return "Unknown Error"
        case .Unknown: return "Unknown"
        }
    }

    public static let allStatuses: [HttpResponseStatus] = [
        .Ok, .Created, .Accepted, .NotAuthoritative, .NoContent, .Reset, .Partial,
        .MultChoice, .MovedPerm, .MovedTemp, .SeeOther, .NotModified, .UseProxy,
        .BadRequest, .Unauthorized, .PaymentRequired, .Forbidden, .NotFound, .BadMethod,
        .NotAcceptable, .ProxyAuth, .ClientTimeout, .Conflict, .Gone, .LengthRequired,
        .PreconFailed, .EntityTooLarge, .ReqTooLong, .UnsupportedType, .ReqTooMany,
        .InternalError, .NotImplemented, .BadGateway, .Unavailable, .GatewayTimeout,
        .Version, .UnknownError, .Unknown,
    ]

    public static func create(_ code: Int) -> HttpResponseStatus {
        allStatuses.first { $0.code == code } ?? .Unknown
    }

    public var description: String {
        "\(code) \(msg)"
    }
}

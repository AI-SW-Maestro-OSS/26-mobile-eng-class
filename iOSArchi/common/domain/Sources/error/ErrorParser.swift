import Foundation

public extension HTTPResponseError {
    func handlingErrorOnUseCase<ErrorType: HTTPErrorType>(_ type: ErrorType.Type = ErrorType.self) -> ErrorType? {
        ErrorType.allCases.first { ($0.type == self.cause?.message) && $0.isHandledOnDomain }
    }

    func isCommonErrorHandling() -> Bool {
        self.rawCode == 401 || self.rawCode == 404 || self.rawCode >= 500
    }
}

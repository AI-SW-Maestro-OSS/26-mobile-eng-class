import Foundation

public func formatKakaoDateTime(_ raw: String) -> String {
    guard !raw.isEmpty else { return "" }

    let isoFormatterWithFractional = ISO8601DateFormatter()
    isoFormatterWithFractional.formatOptions = [.withInternetDateTime, .withFractionalSeconds]

    let isoFormatterStandard = ISO8601DateFormatter()
    isoFormatterStandard.formatOptions = [.withInternetDateTime]

    let date = isoFormatterWithFractional.date(from: raw) ?? isoFormatterStandard.date(from: raw)
    guard let parsedDate = date else { return raw }

    let outputFormatter = DateFormatter()
    outputFormatter.locale = Locale(identifier: "en_US_POSIX")
    outputFormatter.dateFormat = "yyyy.MM.dd HH:mm"
    return outputFormatter.string(from: parsedDate)
}

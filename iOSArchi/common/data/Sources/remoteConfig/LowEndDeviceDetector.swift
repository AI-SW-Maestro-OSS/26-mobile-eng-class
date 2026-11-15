import Foundation

/// Remote Config custom signal(`isLowEndDevice`) 용 저사양 단말 판정.
/// RAM 8GB 미만 이면서 프로세서 코어 수 4개 미만일 때 저사양으로 본다. 판정 실패 시 false.
public enum LowEndDeviceDetector {
    private static let TAG = "RemoteConfigV2"
    private static let LOW_RAM_THRESHOLD_GB: Double = 8.0
    private static let LOW_CORE_COUNT_THRESHOLD: Int = 4

    public static func isLowEndDevice() -> Bool {
        let processInfo = ProcessInfo.processInfo
        let totalRamInGB = Double(processInfo.physicalMemory) / (1024.0 * 1024.0 * 1024.0)
        let processorCount = processInfo.activeProcessorCount

        let isLowRam = totalRamInGB < LOW_RAM_THRESHOLD_GB
        let isLowCpu = processorCount < LOW_CORE_COUNT_THRESHOLD

        print("[\(TAG)] Device specs - RAM: \(String(format: "%.2f", totalRamInGB))GB, Cores: \(processorCount)")
        return isLowRam && isLowCpu
    }
}

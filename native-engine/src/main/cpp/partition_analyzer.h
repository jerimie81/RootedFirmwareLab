#pragma once

#include <string>
#include <vector>
#include <unordered_map>

struct FileEntry {
    std::string name;
    std::string type; // "file" or "directory"
    long size;
};

class PartitionAnalyzer {
public:
    PartitionAnalyzer(const std::string& imagePath);
    std::vector<FileEntry> listFiles(const std::string& path);
    bool extractFile(const std::string& path, const std::string& dest);

private:
    std::string imagePath;
    // Low-level filesystem interaction (e.g., calling ext4/f2fs tools or custom implementation)
};

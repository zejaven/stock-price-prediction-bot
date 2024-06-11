package org.zeveon.stockpricepredictionbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zeveon.stockpricepredictionbot.service.FileService;

import java.io.File;
import java.nio.file.Paths;

/**
 * @author Stanislav Vafin
 */
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    @Value("${stock-price-prediction-bot.video-path}")
    private String videoPath;

    @Override
    public File getVideo(String filename) {
        return Paths.get(videoPath)
                .resolve(filename)
                .toFile();
    }
}

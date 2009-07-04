package org.drools.solver.examples.common.persistence;

import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;

import org.apache.commons.io.IOUtils;
import org.drools.solver.examples.common.app.LoggingMain;
import org.drools.solver.examples.common.persistence.XstreamSolutionDaoImpl;
import org.drools.solver.core.solution.Solution;

/**
 * @author Geoffrey De Smet
 */
public abstract class AbstractOutputConvertor extends LoggingMain {

    private static final String DEFAULT_INPUT_FILE_SUFFIX = ".xml";
    protected static final String DEFAULT_OUTPUT_FILE_SUFFIX = ".txt";

    protected File getInputDir() {
        return new File("data/" + getExampleDirName() + "/solved/");
    }

    protected File getOutputDir() {
        return new File("data/" + getExampleDirName() + "/output/");
    }

    protected abstract String getExampleDirName();

    protected String getInputFileSuffix() {
        return DEFAULT_INPUT_FILE_SUFFIX;
    }

    protected String getOutputFileSuffix() {
        return DEFAULT_OUTPUT_FILE_SUFFIX;
    }

    public void convertAll() {
        File inputDir = getInputDir();
        File outputDir = getOutputDir();
        XstreamSolutionDaoImpl solutionDao = new XstreamSolutionDaoImpl();
        File[] inputFiles = inputDir.listFiles();
        if (inputFiles == null) {
            throw new IllegalArgumentException(
                    "Your working dir should be drools-solver-examples and contain: " + inputDir);
        }
        for (File inputFile : inputFiles) {
            String inputFileName = inputFile.getName();
            if (inputFileName.endsWith(getInputFileSuffix())) {
                Solution solution = solutionDao.readSolution(inputFile);
                String outputFileName = inputFileName.substring(0,
                        inputFileName.length() - getInputFileSuffix().length())
                        + getOutputFileSuffix();
                File outputFile = new File(outputDir, outputFileName);
                writeSolution(solution, outputFile);
            }
        }
    }

    public abstract OutputBuilder createOutputBuilder();

    public void writeSolution(Solution solution, File outputFile) {
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
            OutputBuilder outputBuilder = createOutputBuilder();
            outputBuilder.setBufferedWriter(bufferedWriter);
            outputBuilder.setSolution(solution);
            outputBuilder.writeSolution();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        } finally {
            IOUtils.closeQuietly(bufferedWriter);
        }
    }

    public abstract class OutputBuilder {

        protected BufferedWriter bufferedWriter;

        public void setBufferedWriter(BufferedWriter bufferedWriter) {
            this.bufferedWriter = bufferedWriter;
        }
        
        public abstract void setSolution(Solution solution);

        public abstract void writeSolution() throws IOException;

        // ************************************************************************
        // Helper methods
        // ************************************************************************

    }

}
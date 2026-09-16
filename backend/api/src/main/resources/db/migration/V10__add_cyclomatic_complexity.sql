ALTER TABLE source_structure_metrics ADD COLUMN cyclomatic_complexity INTEGER NOT NULL DEFAULT 1;
ALTER TABLE source_structure_metrics ADD COLUMN maximum_method_complexity INTEGER NOT NULL DEFAULT 0;

CREATE TABLE method_complexity_metrics (
    id UUID PRIMARY KEY,
    analysis_id UUID NOT NULL REFERENCES repository_analyses(id) ON DELETE CASCADE,
    file_path VARCHAR(2048) NOT NULL,
    language VARCHAR(64) NOT NULL,
    symbol_name VARCHAR(512) NOT NULL,
    symbol_kind VARCHAR(32) NOT NULL,
    start_line INTEGER NOT NULL,
    end_line INTEGER NOT NULL,
    line_count INTEGER NOT NULL,
    nesting_depth INTEGER NOT NULL,
    cyclomatic_complexity INTEGER NOT NULL,
    CONSTRAINT uk_method_complexity_analysis_file_symbol_line
        UNIQUE (analysis_id, file_path, symbol_name, start_line)
);

CREATE INDEX idx_structure_metric_analysis_complexity
    ON source_structure_metrics(analysis_id, cyclomatic_complexity DESC);
CREATE INDEX idx_method_metric_analysis_complexity
    ON method_complexity_metrics(analysis_id, cyclomatic_complexity DESC);
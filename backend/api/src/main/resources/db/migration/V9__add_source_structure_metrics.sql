CREATE TABLE source_structure_metrics (
    id UUID PRIMARY KEY,
    analysis_id UUID NOT NULL REFERENCES repository_analyses(id) ON DELETE CASCADE,
    file_path VARCHAR(2048) NOT NULL,
    language VARCHAR(64) NOT NULL,
    class_count INTEGER NOT NULL DEFAULT 0,
    interface_count INTEGER NOT NULL DEFAULT 0,
    method_count INTEGER NOT NULL DEFAULT 0,
    function_count INTEGER NOT NULL DEFAULT 0,
    import_count INTEGER NOT NULL DEFAULT 0,
    average_method_length DOUBLE PRECISION NOT NULL DEFAULT 0,
    maximum_method_length INTEGER NOT NULL DEFAULT 0,
    maximum_nesting_depth INTEGER NOT NULL DEFAULT 0,
    control_flow_count INTEGER NOT NULL DEFAULT 0,
    parse_error BOOLEAN NOT NULL DEFAULT FALSE,
    symbols TEXT NOT NULL DEFAULT '[]',
    imports TEXT NOT NULL DEFAULT '[]',
    CONSTRAINT uk_structure_metric_analysis_path UNIQUE (analysis_id, file_path)
);

CREATE INDEX idx_structure_metric_analysis_language
    ON source_structure_metrics(analysis_id, language);